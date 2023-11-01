package com.anvesh.wallstreetcrushers.ui.MainActivity.Profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.datamodels.StockDetails
import com.anvesh.wallstreetcrushers.datamodels.User
import com.anvesh.wallstreetcrushers.ui.LoginRegister.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

class ProfileFragment : Fragment() {

    lateinit var txtUserName: TextView
    lateinit var txtEmail: TextView
    lateinit var txtPhone: TextView
    lateinit var txtMoney: TextView
    lateinit var txtTotalMoney: TextView
    lateinit var recyclerView: RecyclerView

    lateinit var db : FirebaseDatabase
    lateinit var adapter: StockAdapter
    lateinit var stockDetails: ArrayList<StockDetails>
    lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initiate(view)
        setHasOptionsMenu(true)

        return view
    }

    private fun getPrefs() {
        val sharedPrefs = this.requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE)
        user = User(
            sharedPrefs.getString("userId", "NULL")!!,
            sharedPrefs.getString("Name", "NULL")!!,
            sharedPrefs.getString("Email", "NULL")!!,
            sharedPrefs.getString("Phone", "NULL")!!,
            sharedPrefs.getString("Money", "0")!!,
            sharedPrefs.getString("TotalMoney", "0")!!
        )
    }

    private fun initiate(view: View) {
        txtUserName = view.findViewById(R.id.txtUserName)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtPhone = view.findViewById(R.id.txtPhone)
        txtMoney = view.findViewById(R.id.txtMoney)
        txtTotalMoney = view.findViewById(R.id.txtTotalMoney)
        recyclerView = view.findViewById(R.id.recyclerView)

        db = FirebaseDatabase.getInstance()

        getPrefs()
        getStockPurchases()

        txtUserName.text = user.name
        txtEmail.text = user.email
        txtPhone.text = user.phone
        txtMoney.text = "INR. ${user.money}"
        txtTotalMoney.text = "INR. ${user.totalMoney}"
    }

    private fun getStockPurchases() {
        val ref = db.getReference("/Users/${user.userId}/Stocks")
        ref.get().addOnSuccessListener { snapshot ->
            stockDetails = ArrayList()
            snapshot.children.forEach {
                stockDetails.add(it.getValue<StockDetails>()!!)
            }
            fillRecyclerView()
        }.addOnFailureListener {

        }
    }

    private fun fillRecyclerView() {
        adapter = StockAdapter(stockDetails, requireActivity())
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val sharedPrefs = this.requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK))
                startActivity(intent)
            }
            R.id.add_money -> {
                val builder = AlertDialog.Builder(requireActivity())
                val inflator = layoutInflater
                builder.setTitle("Add Amount in INR")
                val dialog = inflator.inflate(R.layout.dialog_box_add_money, null)
                val etAddMoney = dialog.findViewById<EditText>(R.id.etAddMoney)
                builder.setView(dialog)
                builder.setPositiveButton("Add"){ _, _ ->
                    addMoneyToAccount(etAddMoney.text.toString())
                }
                builder.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addMoneyToAccount(amount: String) {
        val sharedPrefs = this.requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE).edit()
        user.money = String.format("%.2f", user.money.toDouble() + amount.toDouble())
        user.totalMoney = String.format("%.2f", user.totalMoney.toDouble() + amount.toDouble())
        sharedPrefs.putString("Money", user.money)
        sharedPrefs.putString("TotalMoney", user.totalMoney)
        sharedPrefs.apply()
        val ref = FirebaseDatabase.getInstance().getReference("Users/${user.userId}")
        ref.child("money").setValue(user.money)
        ref.child("totalMoney").setValue(user.totalMoney)
        txtMoney.text = "INR. ${user.money}"
        txtTotalMoney.text = "INR. ${user.totalMoney}"
    }
}