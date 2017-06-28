package service.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import service.data.domain.rels.Rating;

@RepositoryRestResource(collectionResourceRel = "recommendations", path = "recommendations")
public interface RatingRepository extends
        PagingAndSortingRepository<Rating, Long> {
    @Query(value = "MATCH (n:User)-[r:Rating]->() RETURN r")
    Page<Rating> findAll(Pageable pageable);

    @Query(value = "MATCH (n:User)-[r:Rating]->() WHERE n.knownId = {id} RETURN r")
    Page<Rating> findByUserId(@Param(value = "id") String id, Pageable pageable);

}
