package com.onlystudents.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onlystudents.user.entity.School;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SchoolMapper extends BaseMapper<School> {

    @Update("UPDATE school SET population = population + 1 WHERE id = #{schoolId}")
    int incrementPopulation(@Param("schoolId") Long schoolId);

    @Update("UPDATE school SET population = population - 1 WHERE id = #{schoolId} AND population > 0")
    int decrementPopulation(@Param("schoolId") Long schoolId);

    @Update("UPDATE school SET notes = notes + 1 WHERE id = #{schoolId}")
    int incrementNotes(@Param("schoolId") Long schoolId);

    @Update("UPDATE school SET notes = notes - 1 WHERE id = #{schoolId} AND notes > 0")
    int decrementNotes(@Param("schoolId") Long schoolId);

    @Select("SELECT * FROM school WHERE name = #{name} LIMIT 1")
    School selectByName(@Param("name") String name);
}
