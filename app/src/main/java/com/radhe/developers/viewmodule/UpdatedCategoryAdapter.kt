package com.radhe.developers.viewmodule
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.radhe.developers.R

class UpdatedCategoryAdapter(
    context: Context,
    private val categoryList: List<String>,
    private val selectedCategories: List<String>
) : ArrayAdapter<String>(context, 0, categoryList) {

    private val checkedItems = mutableSetOf<String>()

    init {
        checkedItems.addAll(selectedCategories)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
            viewHolder = ViewHolder()
            viewHolder.checkBox = view.findViewById(R.id.checkBox)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val category = getItem(position)
        viewHolder.checkBox?.text = category

        viewHolder.checkBox?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkedItems.add(category!!)
            } else {
                checkedItems.remove(category)
            }
        }

        // Set the initial state of the checkbox
        viewHolder.checkBox?.isChecked = checkedItems.contains(category)

        return view!!
    }

    fun getCheckedItems(): List<String> {
        return checkedItems.toList()
    }

    private class ViewHolder {
        var checkBox: CheckBox? = null
    }
}
