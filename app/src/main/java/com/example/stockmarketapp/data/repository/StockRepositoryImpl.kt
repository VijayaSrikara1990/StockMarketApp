package com.example.stockmarketapp.data.repository

import com.example.stockmarketapp.data.csv.CSVParser
import com.example.stockmarketapp.data.csv.CompanyListingParser
import com.example.stockmarketapp.data.local.StockDatabase
import com.example.stockmarketapp.data.mapper.toCompanyListing
import com.example.stockmarketapp.data.mapper.toCompanyListingEntity
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
    db: StockDatabase,
    val companyListingParser: CSVParser<CompanyListing>
): StockRepository {

    private val dao = db.dao

    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ) = flow<Resource<List<CompanyListing>>> {
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
            companyListingParser.parse(response.byteStream())
        } catch (e: IOException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't load data"))
            null
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(Resource.Error("Couldn't load data"))
            null
        }
        remoteListings?.let { listings ->
            dao.clearCompanyListings()
            dao.insertCompanyListings(
                listings.map { it.toCompanyListingEntity() }
            )

            emit(Resource.Success(
                dao.searchCompanyListings("")
                    .map { it.toCompanyListing() }
            ))
            emit(Resource.Loading(false))
        }
    }

}