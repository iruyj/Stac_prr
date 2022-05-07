package kr.hs.emirim.w2015.stac_prr.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.hs.emirim.w2015.stac_prr.Model.JournalModel
import kr.hs.emirim.w2015.stac_prr.Repository.JournalRepository
import kr.hs.emirim.w2015.stac_prr.Repository.PlantRepository

class JournalViewModel : ViewModel(){
    var plantNames = MutableLiveData<ArrayList<String>>()
    var allJournalsAsk = MutableLiveData<ArrayList<JournalModel>>()
    var allJournalsDesk = MutableLiveData<ArrayList<JournalModel>>()
    var journals = MutableLiveData<ArrayList<JournalModel>>()
    var bookjournals = MutableLiveData<ArrayList<JournalModel>>()
    var isComplate = MutableLiveData<Boolean>()
    val plantRep = PlantRepository
    val journalRep = JournalRepository
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = Firebase.auth

    init {
        viewModelScope.launch {
            allJournalsAsk = journalRep.getJournalListAsk()
            allJournalsDesk = journalRep.getJournalListDest()
            Log.i("TAG", "오름차순: "+allJournalsAsk.value+"내림차순 : "+allJournalsDesk.value)
        }
    }
    // 식물이름들 가져오기
    fun getPlantName(): MutableLiveData<ArrayList<String>> {
        viewModelScope.launch {
            plantNames = plantRep.getNames()
        }
        return plantNames
    }

    // 전체일지 보여주기 - 오름/내림
    fun getAllJournals(sorted : Boolean): MutableLiveData<ArrayList<JournalModel>> {
        Log.i("getAllJournals", "getAllJournals: sorted"+sorted)
        Log.i("TAG", "오름차순: "+allJournalsAsk.value+"내림차순 : "+allJournalsDesk.value)
        when(sorted){
            true->{return allJournalsAsk}
            else->{return allJournalsDesk}
        }
    }

    // 선택한 일지 가져오긴 - 오름/내림 : name
    fun getJournals(sorted: Boolean, name:String): MutableLiveData<ArrayList<JournalModel>> {
        viewModelScope.launch {
            journals = journalRep.getPlantJournal(sorted, name)
            Log.i("TAG", "getJournals: journals"+journals.value )
        }
        return journals
    }
    
    // 북마크 일지 가져오기
    fun getBookmarks(): MutableLiveData<ArrayList<JournalModel>> {
        viewModelScope.launch {
            bookjournals = journalRep.getBookmarks()
            Log.i("TAG", "getBookmarks: 북마크 가져오기"+bookjournals)
        }
        return bookjournals
    }
    
    //북마크 버튼 클릭
    fun setBookmark(docId: String, isChecked:Boolean){
        viewModelScope.launch {
            journalRep.setBookmark(docId, isChecked)
        }
    }

    // 일지 삭제하기 : docId
    fun deleteJournal(docId: String): MutableLiveData<Boolean> {
        viewModelScope.launch {
            val isSuccess = journalRep.deleteJournalItem(docId)
            if(isSuccess.value == true){
                isComplate.postValue(true)
            }else{
                isComplate.postValue(false)
            }
        }
        return isComplate
    }
}