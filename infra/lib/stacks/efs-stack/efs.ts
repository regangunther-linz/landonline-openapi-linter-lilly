import { aws_efs } from "aws-cdk-lib";
import { Construct } from "constructs";

export interface EfsStackProps {
    performanceMode: aws_efs.PerformanceMode;
    lifecyclePolicy: aws_efs.LifecyclePolicy;
    enableAutomaticBackups: boolean;
    encrypted: boolean;
    kmsKey: string;
    fileSystemName: string;
}

export class EfsStack extends Construct {
    constructor(scope: Construct, id: string, props: EfsStackProps) {
        super(scope, id);


        const fileSystem = new aws_efs.FileSystem(this, "EfsFileSystem", {
            performanceMode: props.performanceMode,
            lifecyclePolicy: props.lifecyclePolicy,
            enableAutomaticBackups: props.enableAutomaticBackups,
            encrypted: props.encrypted,
            kmsKey: props.kmsKey,
            fileSystemName: props.fileSystemName,
        });
    }

}
