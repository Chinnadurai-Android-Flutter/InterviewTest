package com.interviewtest.techTask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    var isLoading = false
    private var mydb: DBHelper? = null
    private var retofitConnection: RetrofitClient? = null
    var listValues: Model.ModelValues = Model.ModelValues()
    private var adapter: RecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mydb = DBHelper(applicationContext)
        retofitConnection = RetrofitClient.instance
        adapter = RecyclerViewAdapter(applicationContext, listValues.data!!)
        recyclerView.setHasFixedSize(true)
        val mLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.adapter = adapter
        shimmerViewContainer.startShimmerAnimation()
        if (Utility.isOnline(applicationContext)) {
            getHeroes(1)
            initScrollListener()
        } else {
            getLocalValuesFromLocal()
        }
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light)
        swipeContainer.setOnRefreshListener { // Your code to refresh the list here.
            if (Utility.isOnline(applicationContext)) {
                getHeroes(1)
                initScrollListener()
            } else {
                getLocalValuesFromLocal()
            }
        }
    }


    //Get values from local db
    private fun getLocalValuesFromLocal() {
        if (mydb!!.getAllCotacts(applicationContext) != null && mydb!!.getAllCotacts(applicationContext).size != 0) {
            listValues.data = mydb!!.getAllCotacts(applicationContext)
            adapter!!.notifyDataSetChanged()
            if (swipeContainer!!.isRefreshing)
                swipeContainer.isRefreshing = false
        } else {
            if (swipeContainer!!.isRefreshing)
                swipeContainer.isRefreshing = false
            shimmerViewContainer.stopShimmerAnimation()
            shimmerViewContainer.visibility = View.GONE
            swipeContainer.visibility = View.VISIBLE

            Toast.makeText(applicationContext, "There no data in local", Toast.LENGTH_LONG).show()
        }
    }


    //To check whether app is now online/offline
    private val networkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
                if (networkInfo != null && networkInfo.detailedState == NetworkInfo.DetailedState.CONNECTED) {
                    showNetworkSnackBar("is Online")

                } else if (networkInfo != null && networkInfo.detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
                    showNetworkSnackBar("is Offline")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        if (networkReceiver != null) unregisterReceiver(networkReceiver)
    }

    private fun initScrollListener() {
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listValues!!.data!!.size - 1) {
                        //bottom of list!
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun loadMore() {
        listValues!!.data!!.add(Model.Data())
        adapter!!.notifyItemInserted(listValues!!.data!!.size - 1)
        listValues!!.data!!.removeAt(listValues!!.data!!.size - 1)
        val scrollPosition = listValues!!.data!!.size
        adapter!!.notifyItemRemoved(scrollPosition)
        getHeroes(listValues!!.page!!.toInt() + 1)
        isLoading = false

    }


    private fun getHeroes(pageNo: Int?) {
        val call: Call<Model.ModelValues> = retofitConnection!!.myApi!!.doGetListResources(pageNo!!.toInt())
        call.enqueue(object : Callback<Model.ModelValues?> {
            override fun onResponse(
                    call: Call<Model.ModelValues?>?,
                    response: Response<Model.ModelValues?>
            ) {
                val heroList: Model.ModelValues = response.body()!!
                listValues.page = heroList.page
                listValues.per_page = heroList.per_page
                listValues.total = heroList.total
                listValues.total_pages = heroList.total_pages
                listValues.support = heroList.support
                listValues.data!!.addAll(heroList.data!!)
                heroList.data!!.forEach { it ->
                    mydb!!.insertContact(it.first_name, it.last_name, it.email, it.avatar, it.id!!.toInt());
                }

                if (swipeContainer!!.isRefreshing)
                    swipeContainer.isRefreshing = false
                adapter!!.notifyDataSetChanged()
                shimmerViewContainer.stopShimmerAnimation()
                shimmerViewContainer.visibility = View.GONE
                swipeContainer.visibility = View.VISIBLE
            }

            override fun onFailure(call: Call<Model.ModelValues?>?, t: Throwable?) {
                shoFailureSnackBar()
            }
        })
    }

    fun showNetworkSnackBar(messageString: String?) {
        val snackbar = Snackbar.make(constarintLayout, messageString!!, Snackbar.LENGTH_LONG)
        val view = snackbar.view
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackbar.show()

        if (Utility.isOnline(applicationContext)) {
            getHeroes(1)
            initScrollListener()
        } else {
            getLocalValuesFromLocal()
        }
    }

    fun shoFailureSnackBar() {
        val snackBar: Snackbar = Snackbar
                .make(constarintLayout, "Fetching failed", Snackbar.LENGTH_LONG)
                .setAction("Try Again") {
                    if (Utility.isOnline(applicationContext)) {
                        getHeroes(1)
                        initScrollListener()
                    } else {
                        getLocalValuesFromLocal()
                    }
                }
        snackBar.show()
    }

}