package com.onlystudents.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.payment.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
    
    @Select("SELECT * FROM payment_order WHERE order_no = #{orderNo}")
    PaymentOrder selectByOrderNo(String orderNo);
    
    @Update("UPDATE payment_order SET status = #{status}, pay_time = NOW(), third_party_no = #{thirdPartyNo} WHERE order_no = #{orderNo}")
    int updatePayStatus(@Param("orderNo") String orderNo, @Param("status") Integer status, @Param("thirdPartyNo") String thirdPartyNo);
    
    @Select("SELECT COUNT(*) > 0 FROM payment_order WHERE user_id = #{userId} AND target_id = #{noteId} AND target_type = 1 AND status = 1")
    boolean checkNotePurchased(@Param("userId") Long userId, @Param("noteId") Long noteId);
    
    @Select("SELECT COALESCE(SUM(creator_amount * 100), 0) FROM payment_order WHERE target_id = #{creatorId} AND status = 1")
    Long selectCreatorRevenue(@Param("creatorId") Long creatorId);
}
