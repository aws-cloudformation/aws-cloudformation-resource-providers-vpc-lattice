# AWS::VpcLattice::Listener Forward

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#targetgroups" title="TargetGroups">TargetGroups</a>" : <i>[ <a href="weightedtargetgroup.md">WeightedTargetGroup</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#targetgroups" title="TargetGroups">TargetGroups</a>: <i>
      - <a href="weightedtargetgroup.md">WeightedTargetGroup</a></i>
</pre>

## Properties

#### TargetGroups

_Required_: Yes

_Type_: List of <a href="weightedtargetgroup.md">WeightedTargetGroup</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

