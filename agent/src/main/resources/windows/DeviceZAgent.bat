IF EXIST DeviceZAgent-update.jar (
    IF EXIST update-lock (
        java -jar DeviceZAgent.jar
    ) else (
        java -jar DeviceZAgent-update.jar
        echo "">update-lock
    )
) else (
    java -jar DeviceZAgent.jar
)