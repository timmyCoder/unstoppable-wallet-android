package io.horizontalsystems.bankwallet.modules.market.category

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.bankwallet.core.App
import io.horizontalsystems.bankwallet.core.iconPlaceholder
import io.horizontalsystems.bankwallet.core.iconUrl
import io.horizontalsystems.bankwallet.modules.market.MarketField
import io.horizontalsystems.core.entities.Currency
import io.horizontalsystems.marketkit.models.MarketInfo
import java.math.BigDecimal

object MarketTopCoinsModule {

    class Factory(private val coinCategoryUid: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val service = MarketTopCoinsService(coinCategoryUid, App.marketKit, App.currencyManager.baseCurrency)
            return MarketTopCoinsViewModel(service, listOf(service)) as T
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Error(val errorText: String) : ViewState()
        data class Data(val items: List<ViewItem>) : ViewState()
    }

    @Immutable
    data class ViewItem(
        val coinName: String,
        val coinCode: String,
        val coinRate: String,
        val coinIconUrl: String,
        val coinIconPlaceholder: Int,
        val marketDataValue: MarketDataValue,
        val rank: String?,
    ) {

        companion object {
            fun create(
                marketInfo: MarketInfo,
                currency: Currency,
                marketField: MarketField
            ): ViewItem {
                val marketDataValue = when (marketField) {
                    MarketField.MarketCap -> {
                        val (shortenValue, suffix) = App.numberFormatter.shortenValue(marketInfo.marketCap)
                        val marketCapFormatted = App.numberFormatter.formatFiat(
                            shortenValue,
                            currency.symbol,
                            0,
                            2
                        ) + " $suffix"

                        MarketDataValue.MarketCap(marketCapFormatted)
                    }
                    MarketField.Volume -> {
                        val (shortenValue, suffix) = App.numberFormatter.shortenValue(marketInfo.totalVolume ?: BigDecimal.ZERO)
                        val volumeFormatted = App.numberFormatter.formatFiat(
                            shortenValue,
                            currency.symbol,
                            0,
                            2
                        ) + " $suffix"

                        MarketDataValue.Volume(volumeFormatted)
                    }
                    MarketField.PriceDiff -> MarketDataValue.Diff(marketInfo.priceChange)
                }
                return ViewItem(
                    marketInfo.fullCoin.coin.name,
                    marketInfo.fullCoin.coin.code,
                    App.numberFormatter.formatFiat(marketInfo.price, currency.symbol, 0, 6),
                    marketInfo.fullCoin.coin.iconUrl,
                    marketInfo.fullCoin.iconPlaceholder,
                    marketDataValue,
                    marketInfo.fullCoin.coin.marketCapRank.toString()
                )
            }
        }
    }

}

sealed class MarketDataValue {
    class MarketCap(val value: String) : MarketDataValue()
    class Volume(val value: String) : MarketDataValue()
    class Diff(val value: BigDecimal?) : MarketDataValue()
}