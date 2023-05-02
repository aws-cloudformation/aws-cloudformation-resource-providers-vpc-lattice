# AWS::VpcLattice::Rule HttpMatch

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#method" title="Method">Method</a>" : <i>String</i>,
    "<a href="#pathmatch" title="PathMatch">PathMatch</a>" : <i><a href="pathmatch.md">PathMatch</a></i>,
    "<a href="#headermatches" title="HeaderMatches">HeaderMatches</a>" : <i>[ <a href="headermatch.md">HeaderMatch</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#method" title="Method">Method</a>: <i>String</i>
<a href="#pathmatch" title="PathMatch">PathMatch</a>: <i><a href="pathmatch.md">PathMatch</a></i>
<a href="#headermatches" title="HeaderMatches">HeaderMatches</a>: <i>
      - <a href="headermatch.md">HeaderMatch</a></i>
</pre>

## Properties

#### Method

_Required_: No

_Type_: String

_Allowed Values_: <code>CONNECT</code> | <code>DELETE</code> | <code>GET</code> | <code>HEAD</code> | <code>OPTIONS</code> | <code>POST</code> | <code>PUT</code> | <code>TRACE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PathMatch

_Required_: No

_Type_: <a href="pathmatch.md">PathMatch</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HeaderMatches

_Required_: No

_Type_: List of <a href="headermatch.md">HeaderMatch</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

