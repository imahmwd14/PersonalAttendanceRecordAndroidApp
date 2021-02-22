package com.mahmoudmohamaddarwish.personalattendancerecord.ui.main

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.input.input
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mahmoudmohamaddarwish.personalattendancerecord.R
import com.mahmoudmohamaddarwish.personalattendancerecord.Record
import com.mahmoudmohamaddarwish.personalattendancerecord.RecordDao
import com.mahmoudmohamaddarwish.personalattendancerecord.databinding.ListItemRecordBinding
import com.mahmoudmohamaddarwish.personalattendancerecord.databinding.MainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "MainFragment"

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.main_fragment) {

    private val viewModel: MainViewModel by viewModels()

    lateinit var binding: MainFragmentBinding

    @Inject
    lateinit var dao: RecordDao

    @Inject
    lateinit var adapter: Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = MainFragmentBinding.bind(requireView())

        binding.rvRecords.adapter = adapter

        dao.getAllRecords().asLiveData().observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (it.isNotEmpty()) {
                binding.tvEmptyDb.visibility = View.INVISIBLE
            } else {
                binding.tvEmptyDb.visibility = View.VISIBLE
            }
        }

        adapter.onDeleteButtonClicked = { record: Record ->
            lifecycleScope.launch(IO) {
                dao.deleteRecord(record)
            }
        }

        adapter.onUpdateButtonClicked = { record: Record ->
            createOrEditRecord(record)
        }

        binding.fabAddRecord.setOnClickListener {
            createOrEditRecord()
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mnu_item_clear_database -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you sure you want to delete all records?")
                    .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                        lifecycleScope.launch(IO) {
                            dao.deleteAllRecords()
                        }
                    }
                    .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->

                    }.show()
                true
            }
            else -> false
        } || super.onOptionsItemSelected(item)
    }

    private fun createOrEditRecord(record: Record? = null) {
        MaterialDialog(requireContext()).show {
            title(text = "Input name for new record")

            input(
                hint = "Event name",
                prefill = record?.name ?: ""
            ) { materialDialog: MaterialDialog, newName: CharSequence ->

                MaterialDialog(requireContext()).show {
                    title(text = "Choose when the event started")
                    dateTimePicker(
                        currentDateTime = Calendar.getInstance().apply {
                            time = Date(record?.startTime ?: System.currentTimeMillis())
                        }
                    ) { _, timeStarted ->

                        MaterialDialog(requireContext()).show {
                            title(text = "Choose when you attended the event")
                            dateTimePicker(
                                currentDateTime = Calendar.getInstance().apply {
                                    time = Date(record?.attendanceTime ?: timeStarted.timeInMillis)
                                }
                            ) { _, timeAttended ->

                                lifecycleScope.launch(IO) {
                                    if (record == null) {
                                        dao.insertRecord(
                                            Record(
                                                newName.toString(),
                                                timeStarted.timeInMillis,
                                                timeAttended.timeInMillis
                                            )
                                        )
                                    } else {
                                        dao.updateRecord(
                                            record.copy(
                                                name = newName.toString(),
                                                startTime = timeStarted.timeInMillis,
                                                attendanceTime = timeAttended.timeInMillis
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            positiveButton(text = "Submit")
            negativeButton(text = "Dismiss")
        }
    }
}

val differ = object : DiffUtil.ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean =
        oldItem == newItem
}

@FragmentScoped
class Adapter @Inject constructor() : ListAdapter<Record, Adapter.Holder>(differ) {

    lateinit var onDeleteButtonClicked: (Record) -> Unit

    lateinit var onUpdateButtonClicked: (Record) -> Unit

    inner class Holder(var binding: ListItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: Record?) = record?.run {
            binding.apply {
                tvName.text = name
                tvAttended.text = SimpleDateFormat.getInstance().format(Date(attendanceTime))
                tvStarted.text = SimpleDateFormat.getInstance().format(Date(startTime))

                btnDelete.setOnClickListener {
                    MaterialAlertDialogBuilder(it.context)
                        .setTitle("Are you sure?")
                        .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                            onDeleteButtonClicked(record)
                        }
                        .setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->

                        }.show()

                }

                btnEdit.setOnClickListener {
                    onUpdateButtonClicked(record)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(
            ListItemRecordBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }
}