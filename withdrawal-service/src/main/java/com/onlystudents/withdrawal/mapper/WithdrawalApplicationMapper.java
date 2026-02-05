package com.onlystudents.withdrawal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.withdrawal.entity.WithdrawalApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WithdrawalApplicationMapper extends BaseMapper<WithdrawalApplication> {
    
    @Select("SELECT * FROM withdrawal_application WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<WithdrawalApplication> selectByUserId(Long userId);
    
    @Select("SELECT * FROM withdrawal_application WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<WithdrawalApplication> selectByStatus(Integer status);
    
    @Select("SELECT SUM(amount) FROM withdrawal_application WHERE user_id = #{userId} AND status = 1 AND deleted = 0")
    java.math.BigDecimal getTotalWithdrawnAmount(Long userId);
}
