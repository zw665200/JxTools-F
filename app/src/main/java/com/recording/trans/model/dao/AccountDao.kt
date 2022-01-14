package com.recording.trans.model.dao

import androidx.room.*
import com.recording.trans.bean.Account

@Dao
interface AccountDao {

    @Query("SELECT * FROM account")
    fun getAll(): List<Account>?

    @Insert
    fun insert(account: Account)

    @Query("SELECT * FROM account WHERE id = (:id)")
    fun find(id: String): Account?

    @Query("SELECT * FROM account WHERE time = (:srcTime)")
    fun findAccountBySrcPath(srcTime: Long): List<Account>?

    @Update
    fun update(vararg accounts: Account)

    @Delete
    fun delete(account: Account)

}