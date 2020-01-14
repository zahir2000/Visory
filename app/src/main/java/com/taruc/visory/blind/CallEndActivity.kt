package com.taruc.visory.blind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.quickblox.users.model.QBUser
import com.taruc.visory.R
import com.taruc.visory.quickblox.db.QbUsersDbManager
import com.taruc.visory.quickblox.utils.Helper
import com.taruc.visory.report.ReportActivity
import com.taruc.visory.utils.*
import kotlinx.android.synthetic.main.activity_call_end.*

class CallEndActivity : AppCompatActivity(), View.OnClickListener {

    private var callHistory: CallHistory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_end)

        callHistory = CallHistory(this)
        callHistory?.setCallerId(Helper.getQbUser().login)
        callHistory?.setCallDate(getCurrentFormattedDateTime())

        storeCallToDb()

        button_bvi_done.setOnClickListener(this)
        button_bvi_report.setOnClickListener(this)
    }

    private fun storeCallToDb() {
        val usersFromDb = QbUsersDbManager.allUsers

        for(user: QBUser in usersFromDb){
            if(user.id.toString()
                    .compareTo(callHistory?.getCalleeId().toString()) == 0){
                callHistory?.setCalleeId(user.login)
                break
            }
        }

        val newCallHistory = CallHistoryClass(
            callHistory?.getCallerId().toString(),
            callHistory?.getCalleeId().toString(),
            callHistory?.getCallDate().toString(),
            callHistory?.getCallTime().toString()
        )

        val uid = FirebaseDatabase.getInstance().getReference("callhistory").push().key
        val rootRef = FirebaseDatabase.getInstance().getReference("callhistory/$uid")
        rootRef.setValue(newCallHistory)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.button_bvi_done -> {
                finish()
            }

            R.id.button_bvi_report -> {
                val intent = Intent(this, ReportActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
