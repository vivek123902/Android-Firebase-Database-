package com.radhe.developers.addModule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.radhe.developers.R

class CategoryAdapter(
    context: Context,
    private val categoryList: List<String>
) : ArrayAdapter<String>(context, 0, categoryList) {

    private val checkedItems = mutableListOf<String>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        }

        val checkBox = view?.findViewById<CheckBox>(R.id.checkBox)
        checkBox?.text = categoryList[position]

        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            val category = categoryList[position]
            if (isChecked) {
                checkedItems.add(category)
            } else {
                checkedItems.remove(category)
            }
        }

        return view!!
    }

    fun getCheckedItems(): List<String> {
        return checkedItems
    }
}
