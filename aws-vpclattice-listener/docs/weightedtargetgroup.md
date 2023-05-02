# AWS::VpcLattice::Listener WeightedTargetGroup

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#targetgroupidentifier" title="TargetGroupIdentifier">TargetGroupIdentifier</a>" : <i>String</i>,
    "<a href="#weight" title="Weight">Weight</a>" : <i>Integer</i>
}
</pre>

### YAML

<pre>
<a href="#targetgroupidentifier" title="TargetGroupIdentifier">TargetGroupIdentifier</a>: <i>String</i>
<a href="#weight" title="Weight">Weight</a>: <i>Integer</i>
</pre>

## Properties

#### TargetGroupIdentifier

_Required_: Yes

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^((tg-[0-9a-z]{17})|(arn:[a-z0-9\-]+:vpc-lattice:[a-zA-Z0-9\-]+:\d{12}:targetgroup/tg-[0-9a-z]{17}))$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Weight

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

