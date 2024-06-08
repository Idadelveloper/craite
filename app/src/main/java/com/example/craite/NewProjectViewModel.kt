package com.example.craite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.Project
import com.example.craite.data.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

