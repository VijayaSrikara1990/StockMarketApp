package com.example.stockmarketapp.data.repository

import com.example.stockmarketapp.data.local.StockDatabase
import com.example.stockmarketapp.data.mapper.toCompanyListing
import com.example.stockmarketapp.data.remote.dto.StockApi
import com.example.stockmarketapp.domain.model.CompanyListing
import com.example.stockmarketapp.domain.repository.StockRepository
import com.example.stockmarketapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase
): StockRepository {

    val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ) = flow {
        emit(Resource.Loading(true))
        val localListings = dao.searchCompanyListings(query).map { it.toCompanyListing() }
        emit(Resource.Success(localListings))

        val isDbEmpty = localListings.isEmpty() && query.isBlank()
        val shouldJustReturnCache = !isDbEmpty && !fetchFromRemote
        if(shouldJustReturnCache) {
            emit(Resource.Loading(false))
        }
        val remoteListings = try {
            val response = api.getListings()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: HttpException) {
            e.printStackTrace()
        }
    }

}