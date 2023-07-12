terraform {
    backend "s3" {
        bucket = "myapp-bucket-epam-final-project"
        key = "myapp/state.tfstate"
        region = "eu-central-1"
    }
}