package kr.hs.emirim.w2015.stac_prr.Fragment

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_plan.*
import kotlinx.android.synthetic.main.fragment_set.*
import kr.hs.emirim.w2015.stac_prr.BuildConfig
import kr.hs.emirim.w2015.stac_prr.DataClass.NoticeData
import kr.hs.emirim.w2015.stac_prr.MainActivity
import kr.hs.emirim.w2015.stac_prr.R
import kr.hs.emirim.w2015.stac_prr.Receiver.AlarmReceiver
import kr.hs.emirim.w2015.stac_prr.Receiver.DeviceBootReceiver
import java.text.SimpleDateFormat
import java.util.*

class SetFragment : Fragment() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private var alarmMgr: AlarmManager? = null
    private lateinit var push: SharedPreferences
    private lateinit var alarmIntent: PendingIntent
    private var isOpen : Boolean? = null
    private var noticeSize : Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        push = context?.getSharedPreferences("push", Context.MODE_PRIVATE)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isOpen = push.getBoolean("isOpen",false)
        noticeSize = push.getInt("noticeSize",0)
        setNoticeDot()  // ??? ????????????
        // ?????? ?????? ??????????????? ????????????
        val isAlarm = push.getBoolean("isAlarm", true)
        set_switch_alarm.isChecked = isAlarm

        // ?????? ????????????
        val versionName = BuildConfig.VERSION_NAME
        set_imgbtn_version.text = versionName

        super.onViewCreated(view, savedInstanceState)
        val main = activity as MainActivity
        set_linear_nugu.setOnClickListener{   //?????? ??????
            main.fragmentChange_for_adapter(SetNuguFragment())
        }
        set_linear_iot.setOnClickListener(){    //???????????? ??????
            main.fragmentChange_for_adapter(SetIotFragment())
        }
        set_linear_notice.setOnClickListener(){ //???????????? ??????
            //???????????? ??? ????????????
            push.edit()
                .putBoolean("isOpen",true)
                .apply()
            if (isOpen ==false){
                set_notice_dot.visibility=View.INVISIBLE
            }
            main.fragmentChange_for_adapter(SetNoticeFragment())
        }
        set_linear_ask.setOnClickListener(){     // ???????????? ??????
            main.fragmentChange_for_adapter(SetAskFragment())
        }
        set_linear_tos.setOnClickListener(){      //???????????? ??????
            main.fragmentChange_for_adapter(SetTosFragment())
        }

        //?????? ???????????? ????????????
        set_switch_alarm.setOnCheckedChangeListener { buttonView, isChecked ->
            var cal: Calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 30)
            }
            var cal2: Calendar = Calendar.getInstance()

            val toastMessage = if (isChecked) {
                with(push.edit()) {
                    putBoolean("isAlarm", true)
                    commit()
                }
                "??????????????? ????????? ????????????"
            } else {
                //?????? ?????????
                val notificationManager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()

                //?????? ????????? ?????????, ???????????? false
                with(push.edit()) {
                    putBoolean("isAlarm", false)
                    commit()
                }
                "?????? ????????? ?????????????????????."
            }
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
        }

    }
    fun setNoticeDot(){
        db.collection("noticed")
            .get()
            .addOnSuccessListener { result ->
                Log.d("TAG", "setNoticeDot: ???????????? ???????????? : ${result.size()} /$noticeSize")
                if (noticeSize < result.size() || noticeSize> result.size()){
                    Log.d("TAG", "setNoticeDot: ???????????? ???????????? ?????? : ${result.size()} / $noticeSize")
                    push.edit()
                        .putInt("noticeSize",result.size())
                        .putBoolean("isOpen",false)
                        .apply()
                    Log.d("TAG", "setNoticeDot: ???????????? : $isOpen")
                    set_notice_dot.visibility=View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(),"???????????? ???????????? ??????", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

}