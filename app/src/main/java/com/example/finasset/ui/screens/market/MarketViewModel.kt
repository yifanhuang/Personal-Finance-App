package com.example.finasset.ui.screens.market

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.finasset.FinAssetApp
import com.example.finasset.data.db.entity.PriceAlertEntity
import com.example.finasset.data.db.entity.WatchItemEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as FinAssetApp

    val watchItems = app.watchItemRepo.allItems
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val alerts = app.priceAlertRepo.allAlerts
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addWatchItem(type: String, code: String, name: String) {
        viewModelScope.launch {
            app.watchItemRepo.insert(WatchItemEntity(assetType = type, code = code, name = name))
        }
    }

    fun removeWatchItem(id: Long) {
        viewModelScope.launch { app.watchItemRepo.deleteById(id) }
    }

    fun addPriceAlert(type: String, code: String, name: String, alertType: String, targetPrice: Double) {
        viewModelScope.launch {
            app.priceAlertRepo.insert(
                PriceAlertEntity(assetType = type, assetCode = code, assetName = name, alertType = alertType, targetPrice = targetPrice)
            )
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch { app.priceAlertRepo.deleteById(id) }
    }
}
