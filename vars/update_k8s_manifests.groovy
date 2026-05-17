#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags
 */

def call(Map config = [:]) {

    def imageTag      = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'

    def gitUserName  = config.gitUserName ?: 'TanUjNimkar'
    def gitUserEmail = config.gitUserEmail ?: 'tanujnimkar2016@gmail.com'

    echo "Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {

        // Configure Git

        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        // Update Kubernetes manifests

        sh """
            # Update main application deployment

            sed -i "s|image: tanuj7777777/cloudnative-commerce-app:.*|image: tanuj7777777/cloudnative-commerce-app:${imageTag}|g" ${manifestsPath}/08-easyshop-deployment.yaml

            # Update migration job

            if [ -f "${manifestsPath}/12-migration-job.yaml" ]; then
                sed -i "s|image: tanuj7777777/cloudnative-commerce-migration:.*|image: tanuj7777777/cloudnative-commerce-migration:${imageTag}|g" ${manifestsPath}/12-migration-job.yaml
            fi

            # Update ingress domain

            if [ -f "${manifestsPath}/10-ingress.yaml" ]; then
                sed -i "s|host: .*|host: cloudnative.tanujdevops.site|g" ${manifestsPath}/10-ingress.yaml
            fi

            # Commit changes if any

            if git diff --quiet; then

                echo "No changes to commit"

            else

                git add ${manifestsPath}/*.yaml

                git commit -m "Update Kubernetes image tags to ${imageTag} [ci skip]"

                # Push to your repository

                git remote set-url origin https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/TanUjNimkar/Production-Grade-Cloud-Native-E-Commerce-Platform.git

                git push origin HEAD:main
            fi
        """
    }
}


