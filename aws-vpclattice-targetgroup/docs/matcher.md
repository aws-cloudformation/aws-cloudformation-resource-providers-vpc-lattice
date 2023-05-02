# AWS::VpcLattice::TargetGroup Matcher

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#httpcode" title="HttpCode">HttpCode</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#httpcode" title="HttpCode">HttpCode</a>: <i>String</i>
</pre>

## Properties

#### HttpCode

_Required_: Yes

_Type_: String

_Minimum Length_: <code>3</code>

_Maximum Length_: <code>2000</code>

_Pattern_: <code>^[0-9-,]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

