package com.aistudy.solver.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudy.solver.data.repository.SolveRepository
import com.aistudy.solver.utils.OcrHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class SolveState {
    object Idle : SolveState()
    object ExtractingText : SolveState()
    object Solving : SolveState()
    data class Success(val question: String, val answer: String) : SolveState()
    data class Error(val message: String) : SolveState()
}

class SolveViewModel : ViewModel() {
    private val repository = SolveRepository()

    private val _uiState = MutableStateFlow<SolveState>(SolveState.Idle)
    val uiState: StateFlow<SolveState> = _uiState

    fun processImage(context: Context, imageUri: Uri) {
        if (_uiState.value is SolveState.ExtractingText || _uiState.value is SolveState.Solving) return
        
        viewModelScope.launch {
            Log.d("SolveViewModel", "Processing image from URI: $imageUri")
            _uiState.value = SolveState.ExtractingText
            try {
                val ocrResult = withContext(Dispatchers.IO) {
                    val ocrHelper = OcrHelper(context)
                    ocrHelper.extractTextFromUri(imageUri)
                }

                ocrResult.onSuccess { questionText ->
                    Log.d("SolveViewModel", "OCR Success: $questionText")
                    solveQuestion(questionText)
                }.onFailure { error ->
                    Log.e("SolveViewModel", "OCR Failure: ${error.message}")
                    _uiState.value = SolveState.Error("OCR Error: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("SolveViewModel", "Critical Error in processImage: ${e.message}")
                _uiState.value = SolveState.Error("Processing Error: ${e.message}")
            }
        }
    }

    fun processBitmap(context: Context, bitmap: android.graphics.Bitmap) {
        if (_uiState.value is SolveState.ExtractingText || _uiState.value is SolveState.Solving) return
        
        viewModelScope.launch {
            Log.d("SolveViewModel", "Processing bitmap")
            _uiState.value = SolveState.ExtractingText
            try {
                val ocrResult = withContext(Dispatchers.IO) {
                    val ocrHelper = OcrHelper(context)
                    ocrHelper.extractTextFromBitmap(bitmap)
                }

                ocrResult.onSuccess { questionText ->
                    Log.d("SolveViewModel", "OCR Success: $questionText")
                    solveQuestion(questionText)
                }.onFailure { error ->
                    Log.e("SolveViewModel", "OCR Failure: ${error.message}")
                    _uiState.value = SolveState.Error("OCR Error: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("SolveViewModel", "Critical Error in processBitmap: ${e.message}")
                _uiState.value = SolveState.Error("Processing Error: ${e.message}")
            }
        }
    }

    private fun solveQuestion(questionText: String) {
        viewModelScope.launch {
            _uiState.value = SolveState.Solving
            Log.d("SolveViewModel", "Solving question: $questionText")
            try {
                val solveResult = withContext(Dispatchers.IO) {
                    repository.getSolution(questionText)
                }
                
                solveResult.onSuccess { answerText ->
                    Log.d("SolveViewModel", "Solving Success")
                    _uiState.value = SolveState.Success(
                        question = questionText,
                        answer = answerText
                    )
                }.onFailure { error ->
                    Log.e("SolveViewModel", "Solving Failure: ${error.message}")
                    _uiState.value = SolveState.Error("AI Error: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e("SolveViewModel", "Critical Error in solveQuestion: ${e.message}")
                _uiState.value = SolveState.Error("Solving Error: ${e.message}")
            }
        }
    }

    fun processText(questionText: String) {
        if (_uiState.value is SolveState.ExtractingText || _uiState.value is SolveState.Solving) return
        solveQuestion(questionText)
    }

    fun reset() {
        Log.d("SolveViewModel", "Resetting solve state")
        _uiState.value = SolveState.Idle
    }
}
