/*
* Copyright (C) 2026 GuidixX
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/

package com.android.kprofiles

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PerAppKprofilesFragment : Fragment() {
    private lateinit var recycler: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.per_app_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recycler = view.findViewById(R.id.per_app_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = Adapter(loadApps())
    }

    private fun loadApps(): List<AppItem> {
        val pm = requireContext().packageManager
        val apps = mutableListOf<AppItem>()
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (ai in packages) {
            if (pm.getLaunchIntentForPackage(ai.packageName) == null) continue
            val label = ai.loadLabel(pm)?.toString() ?: ai.packageName
            val icon = ai.loadIcon(pm)
            apps.add(AppItem(ai.packageName, label, icon))
        }
        apps.sortBy { it.label.lowercase() }
        return apps
    }

    private inner class Adapter(private val data: List<AppItem>) : RecyclerView.Adapter<VH>() {
        private val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        private val disabled = prefs.getStringSet(Constants.KEY_PER_APP_KPROFILES_DISABLED_PACKAGES, emptySet())!!.toMutableSet()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = layoutInflater.inflate(R.layout.per_app_list_item, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = data[position]
            holder.title.text = item.label
            holder.summary.text = item.packageName
            holder.icon.setImageDrawable(item.icon)
            holder.switch.setOnCheckedChangeListener(null)
            val enabledForApp = !disabled.contains(item.packageName)
            holder.switch.isChecked = enabledForApp
            holder.switch.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
                updateAppState(item.packageName, isChecked)
            }

            holder.itemView.setOnClickListener {
                val newState = !holder.switch.isChecked
                holder.switch.isChecked = newState
                updateAppState(item.packageName, newState)
            }
        }

        override fun getItemCount(): Int = data.size

        private fun updateAppState(packageName: String, isEnabled: Boolean) {
            if (isEnabled) disabled.remove(packageName) else disabled.add(packageName)
            prefs.edit().putStringSet(Constants.KEY_PER_APP_KPROFILES_DISABLED_PACKAGES, HashSet(disabled)).apply()
        }
    }

    private class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: android.widget.ImageView = view.findViewById(R.id.app_icon)
        val title: android.widget.TextView = view.findViewById(R.id.app_title)
        val summary: android.widget.TextView = view.findViewById(R.id.app_pkg)
        val switch: android.widget.Switch = view.findViewById(R.id.app_switch)
    }
}

data class AppItem(val packageName: String, val label: String, val icon: android.graphics.drawable.Drawable)
