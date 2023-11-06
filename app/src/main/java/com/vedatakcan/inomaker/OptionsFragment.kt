package com.vedatakcan.inomaker

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.vedatakcan.inomaker.databinding.FragmentOptionsBinding


class OptionsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentOptionsBinding
    private lateinit var navController: NavController


    private val categoryList = ArrayList<CategoriesModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.addMenuProvider(this, viewLifecycleOwner)
        navController = Navigation.findNavController(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOptionsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_options, menu)
    }



    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.addCategory -> {
                // Öğe 1'e tıklanınca yapılacak işlem
                showPasswordDialog()
                return true
            }
            R.id.addImage -> {
                // Öğe 2'ye tıklanınca yapılacak işlem
                return true
            }
        }
        return false
    }

    private fun showPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Şifre Girişi")

        // Şifre girişi için bir EditText alanı ekleyin
        val input = EditText(requireContext())
        builder.setView(input)

        builder.setPositiveButton("Giriş", DialogInterface.OnClickListener { dialog, which ->
            val enteredPassword = input.text.toString()
            val correctPassword = "1984" // Doğru şifreyi burada tanımlayın

            if (enteredPassword == correctPassword) {
                // Şifre doğru, PDF ekleme sayfasına yönlendirme yapabilirsiniz
                navigateToPdfUploadPage()
            } else {
                // Hatalı şifre girişi hakkında bir uyarı gösterin
                showErrorDialog()
            }
        })

        builder.setNegativeButton("İptal", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }

    private fun showErrorDialog() {
        val errorBuilder = AlertDialog.Builder(requireContext())
        errorBuilder.setTitle("Hatalı Şifre")
        errorBuilder.setMessage("Girdiğiniz şifre hatalıdır. Lütfen tekrar deneyin.")
        errorBuilder.setPositiveButton("Tamam", DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        errorBuilder.show()
    }

    private fun navigateToPdfUploadPage() {
        navController.navigate(R.id.action_optionsFragment_to_categoryAndImageAddFragment)
    }


}
