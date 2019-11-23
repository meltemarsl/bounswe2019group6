package com.traderx.ui.profile


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.traderx.R
import com.traderx.api.ErrorHandler
import com.traderx.api.response.FollowerResponse
import com.traderx.ui.search.UserSearchSkeletonRecyclerViewAdapter
import com.traderx.util.FragmentTitleListeners
import com.traderx.util.Helper
import com.traderx.util.Injection
import com.traderx.viewmodel.AuthUserViewModel
import io.reactivex.disposables.CompositeDisposable

class PendingFollowRequestsFragment : Fragment() {

    private lateinit var userViewModel: AuthUserViewModel
    private lateinit var recyclerView: RecyclerView
    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val userViewModelFactory = Injection.provideAuthUserViewModelFactory(context as Context)
        userViewModel =
            ViewModelProvider(this, userViewModelFactory).get(AuthUserViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_pending_follow_requests, container, false)

        recyclerView = root.findViewById<RecyclerView>(R.id.pending_following_requests_list).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = UserSearchSkeletonRecyclerViewAdapter(5)
        }

        disposable.add(
            userViewModel.pendingFollowRequests(context as Context)
                .compose(Helper.applySingleSchedulers<List<FollowerResponse>>())
                .subscribe({
                    recyclerView.adapter = FollowersRecyclerViewAdapter(it) {
                        PendingFollowRequestsFragmentDirections.actionNavigationPendingFollowRequestsToNavigationUser(it)
                    }
                }, {
                    ErrorHandler.handleError(it, context as Context)
                })
        )

        return root
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is FragmentTitleListeners) {
            context.showFragmentTitle(getString(R.string.pending_following_requests))
        }
    }
}
