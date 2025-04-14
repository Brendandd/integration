//package integration.core;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import integration.core.domain.App;
//import integration.core.domain.component.ComponentCategory;
//import integration.core.service.ConfigurationService;
//
///**
// * Unit test for simple App.
// */
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@ContextConfiguration(classes = App.class)
//public class ComponentTest
//{
//	
//	@Autowired
//	private ConfigurationService configurationService;
//   
//	@Test
//	public void testAddNewExternalSystem() {
//		configurationService.addExternalSystem("Epic", "Epic Description");
//		configurationService.addExternalSystem("Healthlink", "Healthlink Description");
//		configurationService.addExternalSystem("HIPS", "HIPS Description");
//	}
//	
//	
//	@Test
//	public void testCreateNewRoute() {
//		
//		
//		long inboundType = configurationService.createComponentType("Component1", "description1", ComponentCategory.INBOUND_COMMUNICATION_POINT);
//		long outboundType = configurationService.createComponentType("Component2", "description2", ComponentCategory.OUTBOUND_COMMUNICATION_POINT);
//		
//		long inboundComponentId = configurationService.createInboundCommunicationPoint("Input1", "Input1Description", inboundType, 1l);
//		long outboundComponentId = configurationService.createOutboundCommunicationPoint("Input2", "Input2Description", outboundType, 1l);
//		
//		long routeId = configurationService.createRoute("Route 3", "Rout1 Description", inboundComponentId, outboundComponentId);
//		assertNotNull(routeId);
//	}
//	
//	
//	@Test
//	public void deleteRoute() {
//		configurationService.removeRoute(1l);
//	}
//}
