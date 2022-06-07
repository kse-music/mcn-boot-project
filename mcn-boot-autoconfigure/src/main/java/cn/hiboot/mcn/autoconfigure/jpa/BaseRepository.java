package cn.hiboot.mcn.autoconfigure.jpa;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * BaseRepository
 *
 * @author DingHao
 * @since 2022/1/22 11:48
 */
@NoRepositoryBean
public interface BaseRepository<T,ID> extends JpaRepositoryImplementation<T, ID> {

}
