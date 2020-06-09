# Using AWS Sam Basics.

## create hello-world app 

```bash
sam init
# select quickstart template
# select 8 using python 3.7
# cloning examples select 1
sam build
# build app
sam deploy --guided
# deploy app to aws
# confirm stack name
# confirm region
# confirm changes before deploy (Y)
# Allow SAM CLI IAM role creation (Y)
# HelloWorldFunction may not have authorization defined, Is this okay? (Y)
# Save arguments to samconfig.toml (Y)
# Deploy this changeset? (Y)
# Successfully created/updated stack
```
## Test endpoint 

curl https://wsddx3yxd9.execute-api.eu-west-2.amazonaws.com/Prod/hello/

# Host Your API Locally

```bash
sam local start-api
# test endpoint output
curl http://127.0.0.1:3000/hello
```
# Delete stack when complete

```bash
aws cloudformation delete-stack --stack-name helloworld-app --region eu-west-2
```


