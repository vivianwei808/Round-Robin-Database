kill -15 `ps -fe|grep "round-robin-database"|awk '{print $2}'`