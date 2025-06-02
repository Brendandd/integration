kubectl create configmap mllp-input-route-config --from-file=config_file=mllp_input_route_config.json --namespace=integration-dev
kubectl create configmap mllp-output-route-config --from-file=config_file=mllp_output_route_config.json --namespace=integration-dev
kubectl create configmap hl7-file-input-route-config --from-file=config_file=hl7_file_input_route_config.json --namespace=integration-dev
kubectl create configmap hl7-file-output-route-config --from-file=config_file=hl7_file_output_route_config.json --namespace=integration-dev