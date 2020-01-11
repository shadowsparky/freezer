package ru.shadowsparky.freezer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope, SearchView.OnQueryTextListener {
    override val coroutineContext = Job() + Dispatchers.IO
    private var adapter: AppsAdapter? = null
    private var originalItems = listOf<AppInfo>()
    private var filteredItems = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        appsRecycler.layoutManager = LinearLayoutManager(this)
        appsRecycler.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        launch(Dispatchers.IO) {
            val apps = AppInfo.getApps(packageManager)
            originalItems = apps
            filteredItems = apps
            setAdapter(apps)
        }
    }

    private fun setAdapter(apps: List<AppInfo>) = runOnUiThread {
        if (adapter != null) {
            adapter?.items = apps
            adapter?.notifyDataSetChanged()
        } else {
            adapter = AppsAdapter(this, WeakReference(applicationContext), apps)
            appsRecycler.adapter = adapter
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null && adapter != null) {
            val matchedItems = filteredItems.filter {
                it.name.toLowerCase(Locale.ROOT).contains(newText.toLowerCase(Locale.ROOT)) ||
                        it.packageName.toLowerCase(Locale.ROOT).contains(newText.toLowerCase(Locale.ROOT))
            }
            setAdapter(matchedItems)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        onQueryTextChange(query)
        return true
    }
    private var searchView: SearchView? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu == null) return false
        menuInflater.inflate(R.menu.menu, menu)
        searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView?.setOnQueryTextListener(this)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        filteredItems = when (item.itemId) {
            R.id.all_item -> originalItems
            R.id.only_enabled -> originalItems.filter { it.isEnabled }
            R.id.only_disabled -> originalItems.filter { !it.isEnabled }
            else -> return false
        }
        onQueryTextChange(searchView?.query.toString())
        item.isChecked = true
        return super.onOptionsItemSelected(item)
    }
}
