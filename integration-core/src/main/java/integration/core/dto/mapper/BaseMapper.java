package integration.core.dto.mapper;

import integration.core.domain.BaseIntegrationDomain;
import integration.core.dto.BaseDto;

/**
 * Base class for all mappers. Maps domain objects to DTOs.
 * 
 * @param <T> The destination type.
 * @param <U> The source type.
 * 
 * @author Brendan Douglas
 */
public abstract class BaseMapper<T extends BaseDto, U extends BaseIntegrationDomain> {

    public abstract T doMapping(U source);
}
