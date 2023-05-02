# AWS::VpcLattice::AuthPolicy

Creates or updates the auth policy.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::VpcLattice::AuthPolicy",
    "Properties" : {
        "<a href="#resourceidentifier" title="ResourceIdentifier">ResourceIdentifier</a>" : <i>String</i>,
        "<a href="#policy" title="Policy">Policy</a>" : <i>Map</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::VpcLattice::AuthPolicy
Properties:
    <a href="#resourceidentifier" title="ResourceIdentifier">ResourceIdentifier</a>: <i>String</i>
    <a href="#policy" title="Policy">Policy</a>: <i>Map</i>
</pre>

## Properties

#### ResourceIdentifier

_Required_: Yes

_Type_: String

_Minimum Length_: <code>21</code>

_Maximum Length_: <code>200</code>

_Pattern_: <code>^((((sn)|(svc))-[0-9a-z]{17})|(arn(:[a-z0-9]+([.-][a-z0-9]+)*){2}(:([a-z0-9]+([.-][a-z0-9]+)*)?){2}:((servicenetwork/sn)|(service/svc))-[0-9a-z]{17}))$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Policy

_Required_: Yes

_Type_: Map

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ResourceIdentifier.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### State

Returns the <code>State</code> value.

