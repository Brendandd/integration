package integration.core.domain.configuration;

/**
 * The types of components.
 */
public enum IntegrationComponentTypeEnum {
    INBOUND_DIRECTORY_ADAPTER(IntegrationComponentCategoryEnum.INBOUND_ADAPTER),
    OUTBOUND_DIRECTORY_ADAPTER(IntegrationComponentCategoryEnum.OUTBOUND_ADAPTER),
    
    INBOUND_MLLP_ADAPTER(IntegrationComponentCategoryEnum.INBOUND_ADAPTER),
    OUTBOUND_MLLP_ADAPTER(IntegrationComponentCategoryEnum.OUTBOUND_ADAPTER),
    
    INBOUND_ROUTE_CONNECTOR(IntegrationComponentCategoryEnum.INBOUND_ROUTE_CONNECTOR),
    OUTBOUND_ROUTE_CONNECTOR(IntegrationComponentCategoryEnum.OUTBOUND_ROUTE_CONNECTOR),
    
    TRANSFORMER(IntegrationComponentCategoryEnum.MESSAGE_HANDLER),
    FILTER(IntegrationComponentCategoryEnum.MESSAGE_HANDLER),
    SPLITTER(IntegrationComponentCategoryEnum.MESSAGE_HANDLER);
    
    private final IntegrationComponentCategoryEnum category;
    
    
    IntegrationComponentTypeEnum(IntegrationComponentCategoryEnum category) {
        this.category = category;
    }
    
    public IntegrationComponentCategoryEnum getCategory() {
        return category;
    }
}
