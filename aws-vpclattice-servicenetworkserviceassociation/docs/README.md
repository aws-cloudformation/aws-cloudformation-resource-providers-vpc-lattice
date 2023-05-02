# AWS::VpcLattice::ServiceNetworkServiceAssociation

Associates a service with a service network.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::VpcLattice::ServiceNetworkServiceAssociation",
    "Properties" : {
        "<a href="#dnsentry" title="DnsEntry">DnsEntry</a>" : <i><a href="dnsentry.md">DnsEntry</a></i>,
        "<a href="#servicenetworkidentifier" title="ServiceNetworkIdentifier">ServiceNetworkIdentifier</a>" : <i>String</i>,
        "<a href="#serviceidentifier" title="ServiceIdentifier">ServiceIdentifier</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::VpcLattice::ServiceNetworkServiceAssociation
Properties:
    <a href="#dnsentry" title="DnsEntry">DnsEntry</a>: <i><a href="dnsentry.md">DnsEntry</a></i>
    <a href="#servicenetworkidentifier" title="ServiceNetworkIdentifier">ServiceNetworkIdentifier</a>: <i>String</i>
    <a href="#serviceidentifier" title="ServiceIdentifier">ServiceIdentifier</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### DnsEntry

_Required_: No

_Type_: <a href="dnsentry.md">DnsEntry</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ServiceNetworkIdentifier

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^((sn-[0-9a-z]{17})|(arn:[a-z0-9\-]+:vpc-lattice:[a-zA-Z0-9\-]+:\d{12}:servicenetwork/sn-[0-9a-z]{17}))$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ServiceIdentifier

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^((svc-[0-9a-z]{17})|(arn:[a-z0-9\-]+:vpc-lattice:[a-zA-Z0-9\-]+:\d{12}:service/svc-[0-9a-z]{17}))$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

#### CreatedAt

Returns the <code>CreatedAt</code> value.

#### DomainName

Returns the <code>DomainName</code> value.

#### HostedZoneId

Returns the <code>HostedZoneId</code> value.

#### Id

Returns the <code>Id</code> value.

#### ServiceNetworkArn

Returns the <code>ServiceNetworkArn</code> value.

#### ServiceNetworkId

Returns the <code>ServiceNetworkId</code> value.

#### ServiceNetworkName

Returns the <code>ServiceNetworkName</code> value.

#### ServiceArn

Returns the <code>ServiceArn</code> value.

#### ServiceId

Returns the <code>ServiceId</code> value.

#### ServiceName

Returns the <code>ServiceName</code> value.

#### Status

Returns the <code>Status</code> value.

