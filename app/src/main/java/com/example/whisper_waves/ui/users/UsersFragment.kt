package com.example.whisper_waves.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.whisper_waves.App

import com.example.whisper_waves.R

import com.example.whisper_waves.data.EventObserver
import com.example.whisper_waves.databinding.FragmentUsersBinding
import com.example.whisper_waves.ui.profile.ProfileFragment


class UsersFragment : Fragment() {

    private val viewModel: UsersViewModel by viewModels { UsersViewModelFactory(App.myUserID) }
    private lateinit var viewDataBinding: FragmentUsersBinding
    private lateinit var listAdapter: UsersListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding =
            FragmentUsersBinding.inflate(inflater, container, false).apply { viewmodel = viewModel }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupListAdapter()
        setupObservers()
    }

    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel
        if (viewModel != null) {
            listAdapter = UsersListAdapter(viewModel)
            viewDataBinding.usersRecyclerView.adapter = listAdapter
        } else {
            throw Exception("The viewmodel is not initialized")
        }
    }

    private fun setupObservers() {
        viewModel.selectedUser.observe(viewLifecycleOwner, EventObserver { navigateToProfile(it.info.id) })
    }

    private fun navigateToProfile(userID: String) {
        val bundle = bundleOf(ProfileFragment.ARGS_KEY_USER_ID to userID)
        findNavController().navigate(R.id.action_navigation_users_to_profileFragment, bundle)
    }
}