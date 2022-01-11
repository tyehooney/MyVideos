package com.tyehooney.myvideos.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.tyehooney.myvideos.R

abstract class BaseFragment<T : ViewDataBinding>(
    @LayoutRes val layoutRes: Int
) : Fragment() {

    private var _binding: T? = null
    protected val binding get() = _binding ?: error(getString(R.string.error_init_binding))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun showToast(str: String) {
        context?.let {
            Toast.makeText(it, str, Toast.LENGTH_SHORT).show()
        }
    }
}