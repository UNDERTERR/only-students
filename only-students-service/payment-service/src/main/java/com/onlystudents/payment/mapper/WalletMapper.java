package com.onlystudents.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.payment.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
    
    @Select("SELECT * FROM wallet WHERE user_id = #{userId}")
    Wallet selectByUserId(Long userId);
    
    @Update("UPDATE wallet SET balance = balance + #{amount}, total_income = total_income + #{amount} WHERE user_id = #{userId}")
    int addIncome(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    @Update("UPDATE wallet SET balance = balance - #{amount}, total_withdrawal = total_withdrawal + #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
