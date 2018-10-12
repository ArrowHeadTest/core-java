#!/bin/sh -e

. /usr/share/debconf/confmodule
. /usr/share/arrowhead/conf/ahconf.sh

db_input critical arrowhead-common/public_mysql || true
db_go || true
db_get arrowhead-common/public_mysql

case ${RET} in
    Yes )
        if [ $(mysql -u root -sse "SELECT EXISTS(SELECT 1 FROM mysql.user WHERE user = 'arrowhead' AND host = '%')") != 1 ]; then
            mysql -e "CREATE USER arrowhead@'%' IDENTIFIED BY '${AH_PASS_DB}';"
            mysql -e "GRANT ALL PRIVILEGES ON arrowhead.* TO arrowhead@'%';"
            mysql -e "FLUSH PRIVILEGES;"
        fi
        sed -i 's/^\(bind-address[ \t]*=[ \t]*\).*$/\10.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
        systemctl restart mysql
        ;;
    No )
        ;;
esac

ah_gen_system client1 127.0.0.1
echo >&2
ah_gen_system SecureTemperatureSensor 127.0.0.1 IndoorTemperature
echo >&2
echo "WARNING: No authorization/orchestration entries will be generated by this version of the script" >&2
# TODO Autogenerated ids
#mysql -u root arrowhead <<EOF
#LOCK TABLES arrowhead_system WRITE, arrowhead_service WRITE, intra_cloud_authorization WRITE, orchestration_store WRITE;
#
#INSERT INTO intra_cloud_authorization VALUES (1, 1, 2, 1);
#INSERT INTO orchestration_store VALUES (1, 'Y', NULL, NOW(), 'Test', 1, 1, NULL, 2, 1);
#
#UNLOCK TABLES;
#EOF
