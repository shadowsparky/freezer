package ru.shadowsparky.freezer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class AppsAdapter(
    private val scope: CoroutineScope,
    private val context: WeakReference<Context>,
    var items: List<AppInfo>
) : RecyclerView.Adapter<AppsAdapter.AppsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.application_item, parent, false)
        return AppsViewHolder(itemView)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: AppsViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    inner class AppsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.appImage)
        private val appInfo: CheckedTextView = itemView.findViewById(R.id.appInfo)
        private val layout: RelativeLayout = itemView.findViewById(R.id.item_layout)
        private val appPackage: TextView = itemView.findViewById(R.id.appPackage)

        fun bind(item: AppInfo, position: Int) {
            image.setImageDrawable(item.drawable)
            appInfo.text = item.name
            appInfo.isChecked = item.isEnabled
            appPackage.text = item.packageName
            layout.setOnClickListener {
                if (appInfo.isChecked) {
                    invokeCommand(item, position, R.string.app_disabled, "disable")
                } else {
                    invokeCommand(item, position, R.string.app_enabled, "enable")
                }
            }
        }

        private fun invokeCommand(appInfo: AppInfo, position: Int, successCode: Int, state: String) = scope.launch {
            val result = Shell.su("pm $state ${appInfo.packageName}").exec()
            if (result.isSuccess) {
                showToast(successCode)
                appInfo.isEnabled = !appInfo.isEnabled
                runOnUiThread { notifyItemChanged(position) }
            } else {
                if (!Shell.rootAccess()) {
                    showToast(R.string.root_not_granted)
                } else {
                    showToast(R.string.unrecognized_error)
                }
            }
        }

        private fun runOnUiThread(block: () -> Unit) {
            scope.launch(Dispatchers.Main) {
                block.invoke()
            }
        }

        private fun showToast(messageCode: Int) = runOnUiThread {
            Toast.makeText(context.get(), messageCode, Toast.LENGTH_SHORT).show()
        }
    }
}