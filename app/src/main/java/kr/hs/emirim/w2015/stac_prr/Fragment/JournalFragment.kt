package kr.hs.emirim.w2015.stac_prr.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_journal.*
import kr.hs.emirim.w2015.stac_prr.Adapter.JournalAdapter
import kr.hs.emirim.w2015.stac_prr.DataClass.JournalData
import kr.hs.emirim.w2015.stac_prr.R
import java.text.SimpleDateFormat


class JournalFragment : Fragment() {
    private val datas = mutableListOf<JournalData>()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = Firebase.auth
    private lateinit var pref: SharedPreferences
    private lateinit var btnArr: List<Button>
    private lateinit var journalAdapter: JournalAdapter
    private lateinit var pNames : ArrayList<String?>
    private var dateSort = false    //초기값 내림차순
    private var name : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        pref = context?.getSharedPreferences("pref", Context.MODE_PRIVATE)!!
        pNames = ArrayList<String?>()
        return inflater.inflate(R.layout.fragment_journal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        journalAdapter = JournalAdapter(requireContext(),activity)
        journal_recycler.setOnScrollListener(onScrollListener)
        val btn1 = view.findViewById<Button>(R.id.plant_name_btn1)
        val btn2 = view.findViewById<Button>(R.id.plant_name_btn2)
        val btn3 = view.findViewById<Button>(R.id.plant_name_btn3)
        val btn4 = view.findViewById<Button>(R.id.plant_name_btn4)
        btnArr = listOf<Button>(btn1, btn2, btn3, btn4)

        //식물이름들 가져오기
        db.collection("plant_info")
            .whereEqualTo("userId", auth.uid)
            .get()
            .addOnSuccessListener {
                for (doc in it) {
                    pNames.add(doc["name"] as String?)
                    Log.d("TAG", "getNames: 일지이름 추가 : $pNames")
                }
                setTab()                // 식물개수만큼 탭 설정하기
            }.addOnFailureListener {
                Log.d("TAG", "getNames: spinner 식물 이름들 보여주기 실패")
            }
        onTabClickListener()    // 탭 클릭할때 리스너

        fab.setOnClickListener() {
            val fragment = NewJournalFragment(); // Fragment 생성
            val bundle = Bundle()
            bundle.putBoolean("isEdit",false)
            bundle.putString("docId", "docId")
            bundle.putString("imgUri", "imgUri")
            fragment.arguments = bundle
            Log.i(bundle.toString(), "onViewCreated: bundle")
            //activity.fragmentChange_for_adapter(AddPlanFragment())
            Log.d(fragment.arguments.toString(), "plus버튼 클릭됨")
            activity?.supportFragmentManager!!.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }   //일지 플러스 버튼

        up_btn.setOnClickListener {
            journal_recycler.scrollToPosition(0)
        }   //맨위로
        down_btn.setOnClickListener {   //맨아래로
            journal_recycler.scrollToPosition(journalAdapter.itemCount - 1)
        }
        journal_date_sort.setOnClickListener(){
            if (dateSort==false){   //내림차순이면 오름차순으로 바꾸기
                journal_date_sort.text ="날짜순↑"
            }else{  //내림차순이면
                journal_date_sort.text ="날짜순↓"
            }
            dateSort = !dateSort
            initRecycler(name)
        }
    }

    fun setTab() {   //탭 개수 정하기
        val pcnt = pref.getInt("PlantCnt", 0)   // 식물개수 가져오기
        Log.d("TAG", "setTab: 현재 식물개수 : $pcnt")
        Log.d("TAG", "setTab: 현재 식물이름: $pNames")
        for (i in 0..pcnt-1) {
            btnArr[i].isClickable = true
            btnArr[i].visibility = View.VISIBLE
            btnArr[i].text = pNames[i]
            if (i==0){
                btnArr[0].isSelected = true
                initRecycler(btnArr[0].text as String)
            }
        }
    }

    fun onTabClickListener() {
        // 버튼 클릭시
        plant_all_btn.setOnClickListener(){
            plant_all_btn.isSelected = !plant_all_btn.isSelected
            if (plant_all_btn.isSelected) {
                plant_name_btn1.isSelected = !plant_name_btn1.isSelected
                plant_name_btn2.isSelected = !plant_name_btn1.isSelected
                plant_name_btn3.isSelected = !plant_name_btn1.isSelected
                plant_name_btn4.isSelected = !plant_name_btn1.isSelected
            }
        }
        plant_name_btn1.setOnClickListener() {
            plant_name_btn1.isSelected = !plant_name_btn1.isSelected
            if (plant_name_btn1.isSelected) {
                plant_name_btn2.isSelected = !plant_name_btn1.isSelected
                plant_name_btn3.isSelected = !plant_name_btn1.isSelected
                plant_name_btn4.isSelected = !plant_name_btn1.isSelected
                name = plant_name_btn1.text as String
                initRecycler(name)
            }
        }
        plant_name_btn2.setOnClickListener() {
            plant_name_btn2.isSelected = !plant_name_btn2.isSelected
            if (plant_name_btn2.isSelected) {
                plant_name_btn1.isSelected = !plant_name_btn2.isSelected
                plant_name_btn3.isSelected = !plant_name_btn2.isSelected
                plant_name_btn4.isSelected = !plant_name_btn2.isSelected
                name = plant_name_btn2.text as String
                initRecycler(name)
            }
        }
        plant_name_btn3.setOnClickListener() {
            plant_name_btn3.isSelected = !plant_name_btn3.isSelected
            if (plant_name_btn3.isSelected) {
                plant_name_btn1.isSelected = !plant_name_btn3.isSelected
                plant_name_btn2.isSelected = !plant_name_btn3.isSelected
                plant_name_btn4.isSelected = !plant_name_btn3.isSelected
                name = plant_name_btn3.text as String
                initRecycler(name)
            }
        }
        plant_name_btn4.setOnClickListener() {
            plant_name_btn4.isSelected = !plant_name_btn4.isSelected
            if (plant_name_btn4.isSelected) {
                plant_name_btn1.isSelected = !plant_name_btn4.isSelected
                plant_name_btn2.isSelected = !plant_name_btn4.isSelected
                plant_name_btn3.isSelected = !plant_name_btn4.isSelected
                name = plant_name_btn4.text as String
                initRecycler(name)
            }
        }
    }

    val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                fab.hide()
            } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                fab.show()
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            fab.show()
        }
    }

    // 일지목록 업데이트
    private fun initRecycler(name: String?) {
        journal_recycler.adapter = journalAdapter

        journalAdapter.datas.clear()
        journalAdapter.notifyDataSetChanged()
        // 일지 목록 리사이클 설정
        if (dateSort==false){   //내림차순으로 가져오기
            db.collection("journals")
                .document(auth.uid.toString())
                .collection("journal")
                .whereEqualTo("name", name)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener {
                    Log.d("", "makeTestItems: 해당 날짜 데이터 가져오기 성공")
                    for (document in it) {
                        val date = document["date"] as Timestamp
                        datas.add(JournalData(
                            document["name"] as String,
                            document["content"] as String,
                            SimpleDateFormat("yy-MM-dd").format(date.toDate()),
                            document["imgUri"] as String?,
                            document.id))
                    }
                    Log.i("파이어베이스데이터", datas.toString());
                    journalAdapter.datas = datas
                    journalAdapter.notifyDataSetChanged()
                }
        }else{  //오름차순으로 가져오기
            db.collection("journals")
                .document(auth.uid.toString())
                .collection("journal")
                .whereEqualTo("name", name)
                .orderBy("date")
                .get()
                .addOnSuccessListener {
                    Log.d("", "makeTestItems: 해당 날짜 데이터 가져오기 성공")
                    for (document in it) {
                        val date = document["date"] as Timestamp
                        datas.add(JournalData(
                            document["name"] as String,
                            document["content"] as String,
                            SimpleDateFormat("yy-MM-dd").format(date.toDate()),
                            document["imgUri"] as String?,
                            document.id))
                    }
                    Log.i("파이어베이스데이터", datas.toString());
                    journalAdapter.datas = datas
                    journalAdapter.notifyDataSetChanged()
                }
        }
    }

}

