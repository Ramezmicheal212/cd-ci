# CI/CD Notes - ECS Fargate Deployment

## Trigger rule

The GitHub Actions workflow is defined in `.github/workflows/deploy-ecs.yml`.
It runs automatically on every push to the `main` branch.

## Pipeline stages

1. Checkout the repository.
2. Set up Java 25 with Maven dependency caching.
3. Run unit tests with `mvn test`.
4. Build the Spring Boot jar with `mvn clean package -DskipTests`.
5. Authenticate to AWS using GitHub repository secrets.
6. Log in to Amazon ECR using the official `aws-actions/amazon-ecr-login` action.
7. Build and push the Docker image to ECR.
8. Render a new ECS task definition using the official ECS render action.
9. Deploy the new task definition revision to the existing ECS service and wait until stable.
10. Smoke test the deployed service with `GET {ALB_URL}/actuator/health`.

## Image versioning

Each image is pushed to ECR with two tags:

- `${{ github.sha }}`: immutable tag for the exact commit deployed.
- `latest`: convenience tag pointing to the most recent successful pipeline image.

The ECS task definition uses the immutable commit SHA image tag. This makes deployments traceable and makes rollback easier.

## AWS authentication

This workflow uses AWS access key secrets because AWS Academy/Lab accounts commonly provide temporary credentials:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`

The repository variable `ALB_URL` stores the deployed Application Load Balancer base URL, for example `http://example-alb-123.us-east-1.elb.amazonaws.com`.

No database password is stored in GitHub Actions. Database credentials stay in AWS Secrets Manager and are injected into the ECS task at runtime by the task definition.

## Minimum IAM permissions

The GitHub Actions AWS identity should be restricted to the project resources only. It needs permissions for:

- ECR login and image push to the team ECR repository.
- ECS task definition registration.
- ECS service update and service description for the target cluster/service.
- `iam:PassRole` only for the ECS task execution/task roles used by this service.

## Rollback

To roll back, redeploy a previous ECS task definition revision that points to the older image tag.

Example:

```bash
aws ecs update-service \
  --cluster hotels-booking-cluster1 \
  --service hotel-task-service-k6yxpd8o \
  --task-definition hotel-task:PREVIOUS_REVISION
```

Another rollback option is to re-run the workflow from a previous commit SHA or push a new commit that restores the previous application state.

## Submission evidence

Include screenshots of:

- A successful GitHub Actions run.
- ECR showing the pushed commit SHA tag and `latest` tag.
- ECS service events showing the deployment completed and the new task definition revision is running.
- Browser or terminal proof that `{ALB_URL}/actuator/health` returns HTTP 200 with `{"status":"UP"}`.

Deployed ALB base URL:

```text
TODO: paste your ALB URL here
```
