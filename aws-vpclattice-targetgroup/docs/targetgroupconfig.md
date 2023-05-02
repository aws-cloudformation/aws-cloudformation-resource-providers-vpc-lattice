# AWS::VpcLattice::TargetGroup TargetGroupConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#port" title="Port">Port</a>" : <i>Integer</i>,
    "<a href="#protocol" title="Protocol">Protocol</a>" : <i>String</i>,
    "<a href="#protocolversion" title="ProtocolVersion">ProtocolVersion</a>" : <i>String</i>,
    "<a href="#ipaddresstype" title="IpAddressType">IpAddressType</a>" : <i>String</i>,
    "<a href="#vpcidentifier" title="VpcIdentifier">VpcIdentifier</a>" : <i>String</i>,
    "<a href="#healthcheck" title="HealthCheck">HealthCheck</a>" : <i><a href="healthcheckconfig.md">HealthCheckConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#port" title="Port">Port</a>: <i>Integer</i>
<a href="#protocol" title="Protocol">Protocol</a>: <i>String</i>
<a href="#protocolversion" title="ProtocolVersion">ProtocolVersion</a>: <i>String</i>
<a href="#ipaddresstype" title="IpAddressType">IpAddressType</a>: <i>String</i>
<a href="#vpcidentifier" title="VpcIdentifier">VpcIdentifier</a>: <i>String</i>
<a href="#healthcheck" title="HealthCheck">HealthCheck</a>: <i><a href="healthcheckconfig.md">HealthCheckConfig</a></i>
</pre>

## Properties

#### Port

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Protocol

_Required_: Yes

_Type_: String

_Allowed Values_: <code>HTTP</code> | <code>HTTPS</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProtocolVersion

_Required_: No

_Type_: String

_Allowed Values_: <code>HTTP1</code> | <code>HTTP2</code> | <code>GRPC</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IpAddressType

_Required_: No

_Type_: String

_Allowed Values_: <code>IPV4</code> | <code>IPV6</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### VpcIdentifier

_Required_: Yes

_Type_: String

_Minimum Length_: <code>5</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^vpc-(([0-9a-z]{8})|([0-9a-z]{17}))$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HealthCheck

_Required_: No

_Type_: <a href="healthcheckconfig.md">HealthCheckConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

