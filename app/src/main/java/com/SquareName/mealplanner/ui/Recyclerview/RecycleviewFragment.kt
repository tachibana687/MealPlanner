package com.SquareName.mealplanner.ui.Recyclerview

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.SquareName.mealplanner.GetRecipe.Item
import com.SquareName.mealplanner.GetRecipe.createService
import com.SquareName.mealplanner.R
import com.SquareName.mealplanner.Realms.taskData
import com.SquareName.mealplanner.Realms.testTask
import com.SquareName.mealplanner.WebViewActivity
import io.realm.Realm
import io.realm.RealmList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class RecycleviewFragment : Fragment() {

    private val recipeInterface by lazy { createService() }

    //RecycleView格納変数
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val realm: Realm by lazy {
        Realm.getDefaultInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_recyclerview, container, false)

        recipeInterface.items().enqueue(object : Callback<List<Item>> {
            override fun onFailure(call: Call<List<Item>>?, t: Throwable?) {
                // Log表示(通信失敗)
                Log.d("fetchItems", "response fail")
                Log.d("fetchItems", "throwable :$t")
            }

            override fun onResponse(call: Call<List<Item>>?, response: Response<List<Item>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        // Log表示(成功)
                        Log.d("fetchItems", "response success")
                        //ここにRicycleviewの処理
                        viewAdapter =
                            RecyclerAdapter(it, object : RecyclerAdapter.OnItemClickListener {
                                override fun onItemClick(
                                    view: View,
                                    position: Int,
                                    clickedText: String
                                ) {
                                    ItemClick(view, position, clickedText)
                                    deleteAll()
                                    create("TITLE", clickedText, true)
                                    read(true)
                                    //tmSearch("TITLE")
                                    //Log.d("DB InputCheck",realm.where(testTask::class.java).findAll().toString())
                                }
                            })
                        viewManager = LinearLayoutManager(context)

                        with(root) {
                            recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
                                // 1.adapterにセット
                                adapter = viewAdapter
                                // 2.LayoutMangerをセット
                                layoutManager = viewManager
                            }
                        }
                    }
                }
                // Log表示(ResponseBodyがない)
                Log.d("fetchItems", "response code:" + response.code())
                Log.d("fetchItems", "response errorBody:" + response.errorBody())
            }
        })


//        val root = inflater.inflate(R.layout.fragment_recyclerview, container, false)
//        val value = resources.getStringArray(R.array.URL)
//
//        viewAdapter = RecyclerAdapter(value, object : RecyclerAdapter.OnItemClickListener{
//            override fun onItemClick(view: View, position: Int, clickedText: String) {
//                ItemClick(view, position, clickedText)
//            }
//        })
//        viewManager = LinearLayoutManager(context)
//
//        with(root) {
//            recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
//                // 1.adapterにセット
//                adapter = viewAdapter
//                // 2.LayoutMangerをセット
//                layoutManager = viewManager
//            }
//        }

        return root
    }

//    private fun fetchRecipes() {
//        recipeInterface.items().enqueue(object : Callback<List<Item>> {
//            override fun onFailure(call: Call<List<Item>>?, t: Throwable?) {
//                // Log表示(通信失敗)
//                Log.d("fetchItems", "response fail")
//                Log.d("fetchItems", "throwable :$t")
//            }
//
//            override fun onResponse(call: Call<List<Item>>?, response: Response<List<Item>>) {
//                if (response.isSuccessful) {
//                    response.body()?.let {
//                        // Log表示(成功)
//                        Log.d("fetchItems", "response success")
//
//                        //ここに処理
//
//                    }
//                }
//                // Log表示(ResponseBodyがない)
//                Log.d("fetchItems", "response code:" + response.code())
//                Log.d("fetchItems", "response errorBody:" + response.errorBody())
//            }
//        })
//    }

    //リストをクリックしたときの処理
    fun ItemClick(view: View, position: Int, clickedText: String) {
//        val url = view.itemTextView.text
        val url = clickedText
        val intent = Intent(activity, WebViewActivity::class.java)
        intent.putExtra("url", url)
        this.startActivity(intent)
    }

    fun create(title:String = "", url:String = "", BkmorDia:Boolean){
        realm.executeTransaction{
            var task = realm.createObject(testTask::class.java, UUID.randomUUID().toString())
            task.title = title
            task.url = url
            task.tag = BkmorDia
        }
    }

    fun read(BkmorDia: Boolean){
        var task = realm.where(testTask::class.java).equalTo("tag",BkmorDia).findAll()
        Log.d("InputCheck", task.toString())
//realmResult->arraylist
//        val stepEntryResults: RealmResults<testTask> = task//realm.where(testTask::class.java).findAll()
//        val stepEntryArray: ArrayList<testTask> = ArrayList<testTask>(stepEntryResults)
    }

    fun tmSearch(tmStamp:String){
        var task = realm.where(testTask::class.java).equalTo("title",tmStamp).findAll()
        Log.d("InputCheck", task.toString())
    }

//    fun create(imageId:String = "", recipeName:String = "", recipeUrl:String="", meal:String=""){
//        realm.executeTransaction {
//            var task = realm.createObject(Task::class.java , UUID.randomUUID().toString())
//            task.imageId = imageId
//            task.recipeName = recipeName
//            task.recipeUrl = recipeUrl
//            task.meal = meal
//        }
//    }

    fun delete(id: String) {
        realm.executeTransaction {
            val task = realm.where(testTask::class.java).equalTo("id", id).findFirst()
                ?: return@executeTransaction
            task.deleteFromRealm()
        }
    }

    fun deleteAll() {
        realm.executeTransaction {
            realm.deleteAll()
        }
    }

}
