package integration.core.domain.configuration;

/**
 * The types of components.
 */
public enum ComponentTypeEnum {
    INBOUND_DIRECTORY_ADAPTER(ComponentCategoryEnum.INBOUND_ADAPTER),
    OUTBOUND_DIRECTORY_ADAPTER(ComponentCategoryEnum.OUTBOUND_ADAPTER),
    
    INBOUND_MLLP_ADAPTER(ComponentCategoryEnum.INBOUND_ADAPTER),
    OUTBOUND_MLLP_ADAPTER(ComponentCategoryEnum.OUTBOUND_ADAPTER),
    
    INBOUND_ROUTE_CONNECTOR(ComponentCategoryEnum.INBOUND_ROUTE_CONNECTOR),
    OUTBOUND_ROUTE_CONNECTOR(ComponentCategoryEnum.OUTBOUND_ROUTE_CONNECTOR),
    
    TRANSFORMER(ComponentCategoryEnum.MESSAGE_HANDLER),
    FILTER(ComponentCategoryEnum.MESSAGE_HANDLER),
    SPLITTER(ComponentCategoryEnum.MESSAGE_HANDLER);
    
    private final ComponentCategoryEnum category;
    
    
    ComponentTypeEnum(ComponentCategoryEnum category) {
        this.category = category;
    }
    
    public ComponentCategoryEnum getCategory() {
        return category;
    }
}
