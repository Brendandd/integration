INSERT INTO `component` VALUES 
(1,'From-Adelaide-Hospital-Directory-Inbound-Adapter','1','System',NOW()),
(2,'To-Sydney-Hospital-Directory-Outbound-Adapter','2','System',NOW()),
(3,'From-Adelaide-Hospital-MLLP-Inbound-Adapter','3','System',NOW()),  *
(4,'To-Sydney-Hospital-MLLP-Outbound-Adapter','4','System',NOW()),
(5,'Transform-to-version-2-5','7','System',NOW()),
(6,'Split-Based-on-OBX-Segment','9','System',NOW()),
(7,'Allow-only-ADT-A04','8','System',NOW()),
(8,'To-Other-Hospital-MLLP-Route-Connector','5','System',NOW()), *
(9,'From-Adelaide-Hospital-Route-Connector','6','System',NOW()),
(11,'To-Melbourne-Hospital-MLLP-Outbound-Adapter','4','System',NOW()), *
(12,'From-Adelaide-Hospital-Directory-Route-Connector','4','System',NOW()),


INSERT INTO `component_route` VALUES 
(1,1,4,'RUNNING','RUNNING',1,'System',NOW()),
(2,2,5,'RUNNING','RUNNING',1,'System',NOW()),
(3,3,1,'RUNNING','RUNNING',1,'System',NOW()),  
(4,4,2,'RUNNING','RUNNING',1,'System',NOW()),
(5,5,2,'RUNNING','RUNNING',1,'System',NOW()),
(6,6,2,'RUNNING','RUNNING',1,'System',NOW()),
(7,7,2,'RUNNING','RUNNING',1,'System',NOW()),
(8,8,1,'RUNNING','RUNNING',1,'System',NOW()),   
(9,8,3,'RUNNING','RUNNING',1,'System',NOW()),
(10,9,2,'RUNNING','RUNNING',1,'System',NOW()),
(11,9,4,'RUNNING','RUNNING',1,'System',NOW()),
(12,10,2,'RUNNING','RUNNING',1,'System',NOW()),
(13,11,6,'RUNNING','RUNNING',1,'System',NOW()), 
(14,7,6,'RUNNING','RUNNING',1,'System',NOW()), 
(14,9,6,'RUNNING','RUNNING',1,'System',NOW()),
(14,12,6,'RUNNING','RUNNING',1,'System',NOW());



INSERT INTO `component_type` VALUES 
(1,'Inbound Directory Adapter','Inbound Adapter','System',NOW()),
(2,'Outbound Directory Adapter','Outbound Adapter','System',NOW()),
(3,'MLLP Inbound Adapter','Inbound Adapter','System',now()),
(4,'MLLP Outbound Adapter','Outbound Adapter','System',NOW()),
(5,'Outbound Route Connector','Outbound Route Connector','System',NOW()),
(6,'Inbound Route Connector','Inbound Route Connector','System',NOW()),
(7,'Transformer','Message Handler','System',NOW()),
(8,'Filter','Message Handler','System',NOW()),
(9,'Splitter','Message Handler','System',NOW());


INSERT INTO `external_system` VALUES 
(1,'Adelaide Hospital','System',NOW()),
(2,'Sydney Hospital','System',NOW()),
(3,'Perth Hospital','System',NOW()),
(4,'Melbourne Hospital','System',NOW());


INSERT INTO `route` VALUES 
(1,'Inbound-MLLP-from-Adelaide-Hospital','System',NOW()),
(2,'Outbound-MLLP-to-Sydney-Hospital','System',NOW()),
(3,'Inbound-Directory-from-Adelaide-Hospital','System',NOW()),
(4,'Outbound-Directory-to-Sydney-Hospital','System',NOW()),
(5,'Outbound-MLLP-to-Perth-Hospital','System',NOW()),
(6,'Outbound-MLLP-to-Melbourne-Hospital','System',NOW());



INSERT INTO `camel_messageprocessed_seq` (sequence_name, next_val) VALUES ('default', 1);