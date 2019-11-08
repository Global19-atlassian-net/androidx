/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.navigation.dynamicfeatures.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.dynamicfeatures.DynamicExtras
import androidx.navigation.dynamicfeatures.DynamicInstallManager

/**
 * Dynamic feature navigator for Activity destinations.
 */
@Navigator.Name("activity")
class DynamicActivityNavigator(
    activity: Activity,
    private val installManager: DynamicInstallManager
) : ActivityNavigator(activity) {

    override fun navigate(
        destination: ActivityNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ): NavDestination? {
        val extras = navigatorExtras as? DynamicExtras
        if (destination is Destination) {
            val moduleName = destination.moduleName
            if (moduleName != null && installManager.needsInstall(moduleName)) {
                return installManager.performInstall(destination, args, extras, moduleName)
            }
        }
        return super.navigate(
            destination,
            args,
            navOptions,
            if (extras != null) extras.destinationExtras else navigatorExtras
        )
    }

    override fun createDestination(): Destination {
        return Destination(this)
    }

    /**
     * Destination for [DynamicActivityNavigator].
     */
    class Destination : ActivityNavigator.Destination {
        /**
         * The module name of this [Destination]'s dynamic feature module. This has to be the
         * same as defined in the dynamic feature module's AndroidManifest.xml file.
         */
        var moduleName: String? = null

        /**
         * Create a new [Destination] with a [NavigatorProvider].
         * @see ActivityNavigator.Destination
         */
        constructor(navigatorProvider: NavigatorProvider) : super(navigatorProvider)

        /**
         * Create a new [Destination] with an [ActivityNavigator.Destination].
         * @param activityNavigator The Navigator to use for this [Destination].
         */
        constructor(
            activityNavigator: Navigator<out ActivityNavigator.Destination>
        ) : super(activityNavigator)

        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)
            context.withStyledAttributes(attrs, R.styleable.DynamicActivityNavigator) {
                moduleName = getString(R.styleable.DynamicActivityNavigator_moduleName)
            }
        }
    }
}