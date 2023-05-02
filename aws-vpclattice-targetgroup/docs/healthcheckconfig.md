# AWS::VpcLattice::TargetGroup HealthCheckConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#enabled" title="Enabled">Enabled</a>" : <i>Boolean</i>,
    "<a href="#protocol" title="Protocol">Protocol</a>" : <i>String</i>,
    "<a href="#protocolversion" title="ProtocolVersion">ProtocolVersion</a>" : <i>String</i>,
    "<a href="#port" title="Port">Port</a>" : <i>Integer</i>,
    "<a href="#path" title="Path">Path</a>" : <i>String</i>,
    "<a href="#healthcheckintervalseconds" title="HealthCheckIntervalSeconds">HealthCheckIntervalSeconds</a>" : <i>Integer</i>,
    "<a href="#healthchecktimeoutseconds" title="HealthCheckTimeoutSeconds">HealthCheckTimeoutSeconds</a>" : <i>Integer</i>,
    "<a href="#healthythresholdcount" title="HealthyThresholdCount">HealthyThresholdCount</a>" : <i>Integer</i>,
    "<a href="#unhealthythresholdcount" title="UnhealthyThresholdCount">UnhealthyThresholdCount</a>" : <i>Integer</i>,
    "<a href="#matcher" title="Matcher">Matcher</a>" : <i><a href="matcher.md">Matcher</a></i>
}
</pre>

### YAML

<pre>
<a href="#enabled" title="Enabled">Enabled</a>: <i>Boolean</i>
<a href="#protocol" title="Protocol">Protocol</a>: <i>String</i>
<a href="#protocolversion" title="ProtocolVersion">ProtocolVersion</a>: <i>String</i>
<a href="#port" title="Port">Port</a>: <i>Integer</i>
<a href="#path" title="Path">Path</a>: <i>String</i>
<a href="#healthcheckintervalseconds" title="HealthCheckIntervalSeconds">HealthCheckIntervalSeconds</a>: <i>Integer</i>
<a href="#healthchecktimeoutseconds" title="HealthCheckTimeoutSeconds">HealthCheckTimeoutSeconds</a>: <i>Integer</i>
<a href="#healthythresholdcount" title="HealthyThresholdCount">HealthyThresholdCount</a>: <i>Integer</i>
<a href="#unhealthythresholdcount" title="UnhealthyThresholdCount">UnhealthyThresholdCount</a>: <i>Integer</i>
<a href="#matcher" title="Matcher">Matcher</a>: <i><a href="matcher.md">Matcher</a></i>
</pre>

## Properties

#### Enabled

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Protocol

_Required_: No

_Type_: String

_Allowed Values_: <code>HTTP</code> | <code>HTTPS</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProtocolVersion

_Required_: No

_Type_: String

_Allowed Values_: <code>HTTP1</code> | <code>HTTP2</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Port

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Path

_Required_: No

_Type_: String

_Maximum Length_: <code>2048</code>

_Pattern_: <code>(^/[a-zA-Z0-9@:%_+.~#?&/=-]*$|(^$))</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HealthCheckIntervalSeconds

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HealthCheckTimeoutSeconds

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HealthyThresholdCount

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UnhealthyThresholdCount

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Matcher

_Required_: No

_Type_: <a href="matcher.md">Matcher</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

