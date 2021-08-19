/*
 * Librarian
 * Copyright (C) 2021 ParchmentMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.parchmentmc.librarian.forgegradle;

import net.minecraftforge.gradle.mcp.ChannelProvidersExtension;
import net.minecraftforge.gradle.userdev.UserDevExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

public class LibrarianForgeGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(@Nonnull Project project) {
        project.getRepositories().maven(repo -> {
            repo.setName("ParchmentMC");
            repo.setUrl("https://maven.parchmentmc.org/");
            repo.mavenContent(filter -> filter.includeGroupByRegex("org\\.parchmentmc.*"));
        });

        ParchmentChannelProvider parchmentProvider = new ParchmentChannelProvider();
        project.getExtensions().getByType(ChannelProvidersExtension.class).addProvider(parchmentProvider);

        project.afterEvaluate(p -> {
            UserDevExtension userDev = project.getExtensions().getByType(UserDevExtension.class);
            String mappingsChannel = userDev.getMappingChannel().getOrNull();
            String mappingsVersion = userDev.getMappingVersion().getOrNull();
            if (mappingsChannel == null || mappingsVersion == null || !parchmentProvider.getChannels().contains(mappingsChannel))
                return;

            // Resolve the dependency now, so it gets cached when it's needed in dependency locking hell
            // This allows people to use snapshot versions of Parchment by avoiding MavenArtifactDownloader if possible
            try {
                parchmentProvider.getParchmentZip(project, ParchmentMappingVersion.of(mappingsVersion));
            } catch (Exception e) {
                // Swallow any errors and let them happen during actual resolution just in case it somehow fixes itself
            }
        });
    }
}
